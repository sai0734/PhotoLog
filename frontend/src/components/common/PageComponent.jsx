const PageComponent = ({ serverData, movePage }) => {
  return (
    <div className="page-wrap">
      {serverData.prev ? (
        <div
          className="page-move"
          onClick={() => movePage({ page: serverData.prevPage })}
        >
          Prev{" "}
        </div>
      ) : (
        <></>
      )}

      {serverData.pageNumList.map((pageNum) => (
        <div
          key={pageNum}
          className={`page-item ${serverData.current === pageNum ? "page-current" : ""}`}
          onClick={() => movePage({ page: pageNum })}
        >
          {pageNum}
        </div>
      ))}

      {serverData.next ? (
        <div
          className="page-move"
          onClick={() => movePage({ page: serverData.nextPage })}
        >
          Next
        </div>
      ) : (
        <></>
      )}
    </div>
  );
};

export default PageComponent;
