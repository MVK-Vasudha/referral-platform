import { useEffect, useState } from "react";
import Button from "../../../components/ui/Button";
import Input from "../../../components/ui/Input";
import StatusMessage from "../../../components/ui/StatusMessage";
import { updateMyProfile } from "../services/profileService";

const EditProfileForm = ({ profile, onUpdateSuccess }) => {
  const [formData, setFormData] = useState({
    fullName: profile?.fullName || "",
    headline: profile?.headline || "",
    bio: profile?.bio || "",
    experience: profile?.experience || "",
    company: profile?.company || "",
    skills: profile?.skills || "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    setFormData({
      fullName: profile?.fullName || "",
      headline: profile?.headline || "",
      bio: profile?.bio || "",
      experience: profile?.experience || "",
      company: profile?.company || "",
      skills: profile?.skills || "",
    });
  }, [profile]);

  const handleChange = (event) => {
    setFormData((prev) => ({
      ...prev,
      [event.target.name]: event.target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccessMessage("");

    try {
      const updatedProfile = await updateMyProfile(formData);
      onUpdateSuccess(updatedProfile);
      setSuccessMessage("Profile updated successfully");
    } catch (error) {
      setError(error.message || "Failed to update profile");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-6 rounded-lg border border-gray-200 bg-white p-6 shadow-sm space-y-4">
      <div className="space-y-1">
        <h2 className="text-xl font-semibold text-gray-900">Edit Profile</h2>
        <p className="text-sm text-gray-500">Keep your profile current for better referral visibility.</p>
      </div>

      <Input
        label="Full Name"
        name="fullName"
        value={formData.fullName}
        onChange={handleChange}
        placeholder="Enter your full name"
      />

      <Input
        label="Company"
        name="company"
        value={formData.company}
        onChange={handleChange}
        placeholder="Enter your company"
      />

      <Input
        label="Headline"
        name="headline"
        value={formData.headline}
        onChange={handleChange}
        placeholder="Enter your headline"
      />

      <div>
        <label htmlFor="profile-bio" className="mb-2 block text-sm font-medium text-gray-700">
          Bio
        </label>
        <textarea
          id="profile-bio"
          name="bio"
          value={formData.bio}
          onChange={handleChange}
          placeholder="Tell others about your background and goals"
          rows={3}
          className="w-full rounded-md border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-gray-900 focus:ring-2 focus:ring-gray-200"
        />
      </div>

      <div>
        <label htmlFor="profile-experience" className="mb-2 block text-sm font-medium text-gray-700">
          Experience
        </label>
        <textarea
          id="profile-experience"
          name="experience"
          value={formData.experience}
          onChange={handleChange}
          placeholder="Summarize your roles and experience"
          rows={3}
          className="w-full rounded-md border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-gray-900 focus:ring-2 focus:ring-gray-200"
        />
      </div>

      <Input
        label="Skills"
        name="skills"
        value={formData.skills}
        onChange={handleChange}
        placeholder="List your key skills"
      />

      {error && <StatusMessage variant="error">{error}</StatusMessage>}
      {successMessage && <StatusMessage variant="success">{successMessage}</StatusMessage>}

      <Button type="submit" disabled={loading}>
        {loading ? "Updating..." : "Update Profile"}
      </Button>
    </form>
  );
};

export default EditProfileForm;
