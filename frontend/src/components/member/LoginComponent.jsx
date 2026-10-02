import { useState } from "react";
import useCustomLogin from "../../hooks/useCustomLogin";

const initState = {
  email: "",
  pw: "",
};

const LoginComponent = () => {
  const [loginParam, setLoginParam] = useState({ ...initState });

  const { doLogin, moveToPath } = useCustomLogin();

  const handleChange = (e) => {
    setLoginParam({ ...loginParam, [e.target.name]: e.target.value });
  };

  const handleClickLogin = () => {
    // dispatch(login(loginParam));
    // 비동기 호출
    doLogin(loginParam) // loginSlice의 비동기 호출
      .then((data) => {
        console.log(data);
        if (data.error) {
          alert("이메일과 패스워드를 다시 확인하세요");
        } else {
          alert("로그인 성공");
          moveToPath("/");
        }
      });
  };

  return (
    <div>
      <div>
        <div>Login Component</div>
      </div>
      <div>
        <div>
          <div>Email</div>
          <input
            name="email"
            type={"text"}
            value={loginParam.email}
            onChange={handleChange}
          ></input>
        </div>
      </div>
      <div>
        <div>
          <div>Password</div>
          <input
            name="pw"
            type={"password"}
            value={loginParam.pw}
            onChange={handleChange}
          ></input>
        </div>
      </div>
      <div>
        <div>
          <div>
            <button onClick={handleClickLogin}>LOGIN</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginComponent;
