const Input = ({
  label,
  id,
  name,
  type = "text",
  value,
  onChange,
  placeholder,
  className = "",
  inputClassName = "",
  error = "",
  ...props
}) => {
  const inputId = id || name;
  const describedBy = error ? `${inputId}-error` : undefined;
  const baseClasses =
    "w-full rounded-md border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-gray-900 focus:ring-2 focus:ring-gray-200";
  const errorClasses = error ? " border-red-500 focus:border-red-500 focus:ring-red-100" : "";

  return (
    <div className={className}>
      {label && (
        <label htmlFor={inputId} className="mb-2 block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}

      <input
        id={inputId}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        aria-invalid={!!error}
        aria-describedby={describedBy}
        className={`${baseClasses}${errorClasses} ${inputClassName}`.trim()}
        {...props}
      />

      {error && (
        <p id={describedBy} className="mt-1 text-sm text-red-500">
          {error}
        </p>
      )}
    </div>
  );
};

export default Input;
