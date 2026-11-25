import { useEffect, useState } from "react";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import StatusMessage from "../../../components/ui/StatusMessage";
import { getMyProfile } from "../services/profileService";
import EditProfileForm from "./EditProfileForm";

const formatValue = (value) => value || "Not provided";

const formatDate = (value) => {
  if (!value) {
    return "Not provided";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

const ProfilePage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await getMyProfile();
      setProfile(data);
    } catch (error) {
      setError(error.message || "Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  if (loading) {
    return (
      <PageLayout maxWidth="max-w-3xl">
          <StatusMessage variant="info">Loading profile...</StatusMessage>
      </PageLayout>
    );
  }

  if (error) {
    return (
      <PageLayout maxWidth="max-w-3xl">
          <StatusMessage variant="error">{error}</StatusMessage>
      </PageLayout>
    );
  }

  if (!profile) {
    return (
      <PageLayout maxWidth="max-w-3xl">
          <StatusMessage variant="empty">No profile data available.</StatusMessage>
      </PageLayout>
    );
  }

  return (
    <PageLayout maxWidth="max-w-3xl" contentClassName="space-y-6">
        <PageHeader
          title="My Profile"
          description="Review your account details and keep your profile up to date."
        />

        <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm space-y-3">
          <p><strong>Username:</strong> {formatValue(profile.username)}</p>
          <p><strong>Name:</strong> {formatValue(profile.fullName)}</p>
          <p><strong>Email:</strong> {profile.email}</p>
          <p><strong>Headline:</strong> {formatValue(profile.headline)}</p>
          <p><strong>Bio:</strong> {formatValue(profile.bio)}</p>
          <p><strong>Experience:</strong> {formatValue(profile.experience)}</p>
          <p><strong>Company:</strong> {formatValue(profile.company)}</p>
          <p><strong>Skills:</strong> {formatValue(profile.skills)}</p>
          <p><strong>Role:</strong> {formatValue(profile.role)}</p>
          <p><strong>Connections:</strong> {profile.connectionCount ?? 0}</p>
          <p><strong>Joined:</strong> {formatDate(profile.createdAt)}</p>
        </div>

        <EditProfileForm
          profile={profile}
          onUpdateSuccess={(updated) => {
            setProfile(updated);
          }}
        />
    </PageLayout>
  );
};

export default ProfilePage;
