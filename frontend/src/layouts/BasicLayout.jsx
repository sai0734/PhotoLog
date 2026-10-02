import BasicMenu from "../components/menus/BasicMenu";

const BasicLayout = ({ children }) => {
  return (
    <>
      {/* 기존 헤더 대신 BasicMenu*/}
      <BasicMenu />

      <div>
        <main>{children}</main>
      </div>
    </>
  );
};

export default BasicLayout;
