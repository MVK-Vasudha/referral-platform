import { useEffect, useState } from "react";
import Button from "../../../components/ui/Button";
import Input from "../../../components/ui/Input";
import { createReferral } from "../services/referralService";

const ReferralRequestForm = ({
  receiverId,
  defaultCompany = "",
  onSuccess,
  onCancel,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    company: defaultCompany,
    jobTitle: "",
    message: "",
    resumeLink: "",
  });

  useEffect(() => {
    setForm((current) => ({
      ...current,
      company: current.company || defaultCompany,
    }));
  }, [defaultCompany]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    const company = form.company.trim();
    const jobTitle = form.jobTitle.trim();

    if (!company || !jobTitle) {
      setError("Company and job title are required");
      return;
    }

    try {
      setLoading(true);
      await createReferral({
        receiverId,
        company,
        jobTitle,
        message: form.message.trim() || null,
        resumeLink: form.resumeLink.trim() || null,
      });

      setForm({
        company: defaultCompany,
        jobTitle: "",
        message: "",
        resumeLink: "",
      });
      onSuccess?.("Referral request submitted successfully.");
    } catch (err) {
      setError(err.message || "Failed to submit referral request");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="mt-4 space-y-4 rounded-lg border border-gray-200 bg-gray-50 p-4"
    >
      {error && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
          {error}
        </p>
      )}

      <Input
        label="Company"
        name="company"
        value={form.company}
        onChange={handleChange}
        placeholder="Enter company"
      />

      <Input
        label="Job Title"
        name="jobTitle"
        value={form.jobTitle}
        onChange={handleChange}
        placeholder="Enter job title"
      />

      <div>
        <label
          className="mb-2 block text-sm font-medium text-gray-700"
          htmlFor={`referral-message-${receiverId}`}
        >
          Message
        </label>
        <textarea
          id={`referral-message-${receiverId}`}
          name="message"
          value={form.message}
          onChange={handleChange}
          placeholder="Add an optional message"
          className="w-full rounded-md border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-gray-900 focus:ring-2 focus:ring-gray-200"
          rows={3}
        />
      </div>

      <Input
        label="Resume Link"
        name="resumeLink"
        value={form.resumeLink}
        onChange={handleChange}
        placeholder="Paste a resume URL"
      />

      <div className="flex flex-wrap gap-3">
        <Button type="submit" disabled={loading}>
          {loading ? "Submitting..." : "Send Referral Request"}
        </Button>

        {onCancel && (
          <Button
            type="button"
            onClick={onCancel}
            className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
          >
            Cancel
          </Button>
        )}
      </div>
    </form>
  );
};

export default ReferralRequestForm;
