import BasicMenu from "../components/menus/BasicMenu";

const BasicLayout = ({ children }) => {
  return (
    <>
      {/* 기존 헤더 대신 BasicMenu*/}
      <BasicMenu />

      {/* 상단 여백 my-5 제거 */}
      <div className="bg-white my-5 w-full flex flex-col space-y-1 md:flex-row md:space-x-1 md:space-y-0">
        <main className="bg-sky-300 md:w-4/5 lg:w-3/4 px-5 py-5">
          {/* 상단 여백 py-40 변경 flex 제거 */}
          {children}
        </main>
      </div>
    </>
  );
};

export default BasicLayout;
