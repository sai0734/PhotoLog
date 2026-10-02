import { useState } from "react";
import { useSelector } from "react-redux";
import { modifyMember } from "../../api/memberApi";
import useCustomLogin from "../../hooks/useCustomLogin";
import ResultModal from "../common/ResultModal";

const ModifyComponent = () => {
  const loginInfo = useSelector((state) => state.loginSlice);
  const [member, setMember] = useState(() => ({ ...loginInfo, pw: "" }));
  const { moveToLogin } = useCustomLogin();
  const [result, setResult] = useState();

  const handleChange = (e) => {
    setMember({ ...member, [e.target.name]: e.target.value });
  };

  const handleClickModify = () => {
    // eslint-disable-next-line no-unused-vars
    const { pw, ...rest } = member;
    if (member.pw === "" || member.pw === null) {
      modifyMember(rest).then(() => {
        setResult("Modified");
      });
    } else {
      modifyMember(member).then(() => {
        setResult("Modified");
      });
    }
  };

  const closeModal = () => {
    setResult(null);
    moveToLogin();
  };

  return (
    <div>
      {result ? (
        <ResultModal
          title={"회원정보"}
          content={"정보수정완료"}
          callbackFn={closeModal}
        ></ResultModal>
      ) : (
        <></>
      )}
      <div>
        <div>
          <div>Email</div>
          <input
            name="email"
            type={"text"}
            value={member.email}
            readOnly
          ></input>
        </div>
      </div>
      <div>
        <div>
          <div>Password</div>
          <input
            name="pw"
            type={"password"}
            value={member.pw}
            onChange={handleChange}
          ></input>
        </div>
      </div>
      <div>
        <div>
          <div>Nickname</div>
          <input
            name="nickname"
            type={"text"}
            value={member.nickname}
            onChange={handleChange}
          ></input>
        </div>
      </div>
      <div>
        <div>
          <button type="button" onClick={handleClickModify}>
            Modify
          </button>
        </div>
      </div>
    </div>
  );
};

export default ModifyComponent;
