import React, { useEffect, useState } from "react";
import axios from "../setupAxios";

export default function Profile() {
  const [user, setUser] = useState(null);
  const [form, setForm] = useState({ displayName: "", bio: "" });
  const [status, setStatus] = useState(null);

  useEffect(() => {
    axios
      .get("/api/user")
      .then((res) => {
        if (res.data.authenticated) {
          setUser(res.data);
          setForm({
            displayName: res.data.displayName || "",
            bio: res.data.bio || "",
          });
        } else {
          setUser(null);
        }
      })
      .catch(() => setUser(null));
  }, []);

  const handleLogout = () => {
    window.location.href = "http://localhost:8080/api/logout";
  };

  const submit = async (e) => {
    e.preventDefault();
    try {
      const r = await axios.post("/api/profile", form);
      if (r.data.ok) setStatus("Saved");
      else setStatus("Error");
    } catch (e) {
      setStatus("Error");
    }
  };

  if (user === null) {
    return <div style={{ textAlign: "center" }}>Not logged in or loading...</div>;
  }

  return (
    <div style={{ maxWidth: 600, margin: "40px auto" }}>
      <h2>Profile</h2>
      <img src={user.avatarUrl} alt="avatar" width={96} height={96} />
      <p>
        <strong>Email:</strong> {user.email}
      </p>

      {/* ✅ Display the user's current display name */}
      <h3 style={{ marginTop: 16 }}>Welcome, {user.displayName || "User"}!</h3>

      <form onSubmit={submit}>
        <div style={{ marginTop: 16 }}>
          <label>Display name</label>
          <br />
          <input
            value={form.displayName}
            onChange={(e) =>
              setForm({ ...form, displayName: e.target.value })
            }
          />
        </div>
        <div style={{ marginTop: 8 }}>
          <label>Bio</label>
          <br />
          <textarea
            value={form.bio}
            onChange={(e) => setForm({ ...form, bio: e.target.value })}
            rows={4}
          />
        </div>
        <div style={{ marginTop: 8 }}>
          <button type="submit">Save</button>
          <button
            type="button"
            onClick={handleLogout}
            style={{ marginLeft: 8 }}
          >
            Logout
          </button>
        </div>
      </form>

      {status && <p>{status}</p>}
    </div>
  );
}
