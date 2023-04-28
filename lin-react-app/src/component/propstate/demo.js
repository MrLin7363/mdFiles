import React from 'react';

// props和state的传递示例
class WebSite extends React.Component {

  constructor() {
    super();
    this.state = {
      name: "编程狮",
      site: "https://www.w3cschool.cn"
    }
  }

  render() {
    return (
      <div>
        <Name name={this.state.name} />
        <Link site={this.state.site} />
      </div>
    );
  }
}

class Name extends React.Component {
  render() {
    return (
      <h1>{this.props.name}</h1>
    );
  }
}

class Link extends React.Component {
  render() {
    return (
      <a href={this.props.site}>
        {this.props.site}
      </a>
    );
  }
}

export default WebSite

// ReactDOM.render(
//   <WebSite />,
//   document.getElementById('example')
// );