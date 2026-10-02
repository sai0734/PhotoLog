import useCustomLogin from "../../hooks/useCustomLogin";

const LogoutComponent = () => {
  const { doLogout, moveToPath } = useCustomLogin();

  const handleClickLogout = () => {
    doLogout();
    alert("로그아웃되었습니다.");
    moveToPath("/");
  };

  return (
    <div>
      <div>
        <div>Logout Component</div>
      </div>
      <div>
        <div>
          <div>
            <button onClick={handleClickLogout}>LOGOUT</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LogoutComponent;
