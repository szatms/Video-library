import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../services/authService";

function LoginForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      await login(username, password);
      navigate("/home");
    } catch (err) {
      console.error("LOGIN ERROR:", err);
      setError("Login failed. Check your credentials and backend token setup.");
    }
  };

  return (
    <div className="card shadow-lg p-4" style={{ width: "380px", borderRadius: "16px" }}>
      <h2 className="text-center mb-3">Login</h2>

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
          Sign In
        </button>

        {error && (
          <div className="text-danger mt-3 text-center">
            {error}
          </div>
        )}
      </form>
    </div>
  );
}

export default LoginForm;
