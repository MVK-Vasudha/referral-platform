const PageLayout = ({
  children,
  maxWidth = "max-w-6xl",
  centered = false,
  className = "",
  contentClassName = "",
}) => {
  if (centered) {
    return (
      <div className={`auth-page ${className}`.trim()}>
        <div className={`w-full ${maxWidth} ${contentClassName}`.trim()}>{children}</div>
      </div>
    );
  }

  return (
    <div className={`page-wrap ${className}`.trim()}>
      <div className={`mx-auto w-full ${maxWidth} ${contentClassName}`.trim()}>{children}</div>
    </div>
  );
};

export default PageLayout;
