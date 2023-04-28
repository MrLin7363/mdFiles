import React from 'react';

class Home extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
          pageText: "我是Home界面"
        }
      }

      render () {
        return (
          <div id="clock">
            <h1>{this.state.pageText}</h1>
              123
          </div>
        )
      }
}

export default Home