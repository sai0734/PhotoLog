import { useSelector } from "react-redux";
import { Link } from "react-router-dom";

const BasicMenu = () => {
  const loginState = useSelector((state) => state.loginSlice);
  return (
    <nav id="navbar">
      <div>
        <ul className="nav-list">
          <li>
            <Link to={"/"}>Main</Link>
          </li>
        </ul>
      </div>

      <div>
        {!loginState.email ? (
          <div>
            <Link to={"/member/login"}>Login</Link>
          </div>
        ) : (
          <div>
            <Link to={"/member/logout"}>Logout</Link>
          </div>
        )}
      </div>
    </nav>
  );
};
export default BasicMenu;
