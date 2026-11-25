const variantClassNames = {
  error: "border-red-200 bg-red-50 text-red-600",
  success: "border-green-200 bg-green-50 text-green-700",
  empty: "border-gray-200 bg-white text-gray-600 shadow-sm",
  info: "border-gray-200 bg-white text-gray-600 shadow-sm",
};

const StatusMessage = ({
  children,
  variant = "info",
  className = "",
}) => {
  const variantClassName = variantClassNames[variant] || variantClassNames.info;

  return (
    <p
      className={`rounded-md border px-4 py-3 text-sm ${variantClassName} ${className}`.trim()}
    >
      {children}
    </p>
  );
};

export default StatusMessage;
