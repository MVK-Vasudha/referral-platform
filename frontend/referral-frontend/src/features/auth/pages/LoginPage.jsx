import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/useAuth";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import Button from "../../../components/ui/Button";
import Input from "../../../components/ui/Input";
import StatusMessage from "../../../components/ui/StatusMessage";

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
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

    if (!form.username || !form.password) {
      setError("Username and password are required");
      return;
    }

    try {
      setLoading(true);
      await login(form);
      navigate("/");
    } catch (error) {
      setError(error.message || "Login failed");
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
          title="Login"
          description="Access your referral network account."
        />

        {error && <StatusMessage variant="error">{error}</StatusMessage>}

        <Input
          label="Username"
          name="username"
          value={form.username}
          onChange={handleChange}
          placeholder="Enter your username"
          autoComplete="username"
        />

        <Input
          label="Password"
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Enter your password"
          autoComplete="current-password"
        />

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? "Logging in..." : "Login"}
        </Button>
      </form>
    </PageLayout>
  );
};

export default LoginPage;
