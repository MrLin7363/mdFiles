import React from 'react';
import MyTableList from './list/MyTableList';

class Table extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
          pageText: "我是Table列表组件界面"
        }
      }

      render () {
        return (
          <div>
            <h3>{this.state.pageText}</h3>
            <MyTableList/>
          </div>
        )
      }
}
export default Table