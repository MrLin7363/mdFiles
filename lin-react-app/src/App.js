import React from 'react';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import './App.css';
import Home from './component/Home';
import About from './component/About';
import Form from './component/Form';
import Table from './component/Table';
import Clock from './component/interval/Clock'
import WebSite from './component/propstate/demo';
import Index from './component/reflux/components/Index';
import BaiduIframe from './component/iframe/BaiduIframe';

class App extends React.Component {
  render() {
    return (
      <Router>
      <div>
        <ul>
          <li>
            <Link to="/">Home</Link>
          </li>
          <li>
            <Link to="/about">About函数式组件</Link>
          </li>
          <li>
            <Link to="/form">Form</Link>
          </li>
          <li>
            <Link to="/table">Table</Link>
          </li>
          <li>
            <Link to="/clock">Clock-setInterval</Link>
          </li>
          <li>
            <Link to="/porpstate">props和state的传递示例</Link>
          </li>
          <li>
            <Link to="/reflux">reflux示例</Link>
          </li>
          <li>
            <Link to="/baidu">百度嵌入iframe页</Link>
          </li>
        </ul>
        <hr />
        <Route exact path="/" component={Home} />
        <Route exact path="/about" component={About} />
        <Route exact path="/form" component={Form} />
        <Route exact path="/table" component={Table} />
        <Route exact path="/clock" component={Clock} />
        <Route exact path="/porpstate" component={WebSite} />
        <Route exact path="/reflux" component={Index} />
        <Route exact path="/baidu" component={BaiduIframe} />
      </div>
    </Router>
    );
  }
}

export default App;
