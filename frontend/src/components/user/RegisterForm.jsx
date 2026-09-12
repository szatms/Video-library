import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register } from "../../services/authService";
import api from "../../services/api";

function RegisterForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const data = await register(username, password);
      
      // Redirect to init page after successful registration (for first-time setup)
      // User settings will be initialized during the manual init process
      navigate("/init");
    } catch (err) {
      console.error("REGISTER ERROR:", err);
      if (err.response && err.response.data && err.response.data.error) {
        setError(err.response.data.error);
      } else {
        setError("Registration failed. Please try again.");
      }
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

        {error && (
          <div className="text-danger mt-3 text-center">
            {error}
          </div>
        )}
      </form>
    </div>
  );
}

export default RegisterForm;