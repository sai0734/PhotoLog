import { API_SERVER_HOST } from "./memberApi";
import jwtAxios from "../util/jwtUtil";

const host = `${API_SERVER_HOST}/api/board`;

export const getBoardList = async (page, size) => {
  const res = await jwtAxios.get(`${host}/?page=${page}&size=${size}`);

  return res.data;
};

export const getBoard = async (boardNumber) => {
  const res = await jwtAxios.get(`${host}/${boardNumber}`);

  return res.data;
};

export const insert = async (title, contents, files) => {
  const form = new FormData();
  form.append("title", title);
  form.append("contents", contents);

  files.forEach((file) => form.append("files", file));

  const res = await jwtAxios.post(`${host}/`, form);

  return res.data;
};

export const modify = async (
  boardNumber,
  title,
  contents,
  files,
  keepImageNumbers,
) => {
  const form = new FormData();

  form.append("boardNumber", boardNumber);
  form.append("title", title);
  form.append("contents", contents);
  files.forEach((file) => form.append("files", file));
  keepImageNumbers.forEach((keepImageNumber) =>
    form.append("keepImageNumbers", keepImageNumber),
  );

  const res = await jwtAxios.put(`${host}/${boardNumber}`, form);

  return res.data;
};

export const deleteBoard = async (boardNumber) => {
  const res = await jwtAxios.delete(`${host}/${boardNumber}`);

  return res.data;
};
