import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/useAuth";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import Button from "../../../components/ui/Button";
import Input from "../../../components/ui/Input";
import StatusMessage from "../../../components/ui/StatusMessage";

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (event) => {
    setForm((prev) => ({
      ...prev,
      [event.target.name]: event.target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!form.username || !form.email || !form.password) {
      setError("All fields are required");
      return;
    }

    try {
      setLoading(true);
      await register(form);
      navigate("/login");
    } catch (error) {
      setError(error.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageLayout centered maxWidth="max-w-md">
      <form
        onSubmit={handleSubmit}
        className="w-full rounded-lg border border-gray-200 bg-white p-6 shadow-sm space-y-4"
      >
        <PageHeader
          title="Register"
          description="Create your account to start networking."
        />

        {error && <StatusMessage variant="error">{error}</StatusMessage>}

        <Input
          label="Username"
          name="username"
          value={form.username}
          onChange={handleChange}
          placeholder="Choose a username"
          autoComplete="username"
        />

        <Input
          label="Email"
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="Enter your email"
          autoComplete="email"
        />

        <Input
          label="Password"
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Create a password"
          autoComplete="new-password"
        />

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? "Registering..." : "Register"}
        </Button>
      </form>
    </PageLayout>
  );
};

export default RegisterPage;
