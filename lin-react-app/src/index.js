import ReactDOM from 'react-dom';
import App from './App';
import React from 'react';


ReactDOM.render(
  // 开启严格模式react会渲染两次render
  // <React.StrictMode> 
    <App />,
  // </React.StrictMode>,
  document.getElementById('root')
)