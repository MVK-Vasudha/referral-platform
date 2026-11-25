const Button = ({
  type = "button",
  disabled = false,
  children,
  className = "",
  ...props
}) => {
  const baseClasses =
    "inline-flex items-center justify-center rounded-md bg-gray-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-300 disabled:cursor-not-allowed disabled:opacity-60";

  return (
    <button
      type={type}
      disabled={disabled}
      className={`${baseClasses} ${className}`.trim()}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
