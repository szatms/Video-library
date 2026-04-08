import { useState } from "react";
import { register } from "../services/authService";

function RegisterForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const data = await register(username, password);
      console.log("REGISTER OK:", data);
      window.location.reload();
    } catch (err) {
      console.error("REGISTER ERROR:", err);
    }
  };

  return (
    <div className="card shadow-lg p-4" style={{ width: "380px", borderRadius: "16px" }}>
      <h2 className="text-center mb-3">Register</h2>

      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <input
            type="text"
            className="form-control form-control-lg"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </div>

        <div className="mb-4">
          <input
            type="password"
            className="form-control form-control-lg"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button className="btn btn-success w-100 btn-lg">
          Sign Up
        </button>
      </form>
    </div>
  );
}

export default RegisterForm;