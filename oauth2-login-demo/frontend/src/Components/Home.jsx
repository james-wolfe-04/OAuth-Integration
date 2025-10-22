import React from "react";

export default function Home() {
  const goGoogle = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

const goGithub = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/github";
};

  return (
    <div style={{textAlign:'center', marginTop:80}}>
      <h1>OAuth2 Demo</h1>
      <button onClick={goGoogle}>Login with Google</button>
      <button onClick={goGithub} style={{marginLeft:10}}>Login with GitHub</button>
    </div>
  );
}
