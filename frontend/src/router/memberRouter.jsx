import { Suspense, lazy } from "react";
const Loading = <div>Loading....</div>;
const LoginPage = lazy(() => import("../pages/member/LoginPage"));
const LogoutPage = lazy(() => import("../pages/member/LogoutPage"));
const JoinPage = lazy(() => import("../pages/member/JoinPage"));
const ModifyPage = lazy(() => import("../pages/member/ModifyPage"));

const memberRouter = () => {
  return [
    {
      path: "login",
      element: (
        <Suspense fallback={Loading}>
          <LoginPage />
        </Suspense>
      ),
    },
    {
      path: "logout",
      element: (
        <Suspense fallback={Loading}>
          <LogoutPage />
        </Suspense>
      ),
    },
    {
      path: "join",
      element: (
        <Suspense fallback={Loading}>
          <JoinPage />
        </Suspense>
      ),
    },
    {
      path: "modify",
      element: (
        <Suspense fallback={Loading}>
          <ModifyPage />
        </Suspense>
      ),
    },
  ];
};

export default memberRouter;
