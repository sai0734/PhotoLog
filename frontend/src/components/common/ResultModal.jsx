const ResultModal = ({ title, content, callbackFn }) => {
  return (
    <div
      className="modal-overlay"
      onClick={() => {
        if (callbackFn) {
          callbackFn();
        }
      }}
    >
      <div className="result-box">
        <div className="result-title">{title}</div>
        <div className="result-content">{content}</div>
        <div className="result-footer">
          <button
            className="result-button"
            onClick={() => {
              if (callbackFn) {
                callbackFn();
              }
            }}
          >
            Close Modal
          </button>
        </div>
      </div>
    </div>
  );
};

export default ResultModal;
