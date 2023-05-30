import React from 'react';
import EssayForm from './form/EssayForm';
import FlavorForm from './form/FlavorForm';
import NameForm from './form/NameForm';
import Reservation from './form/Reservation';

class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pageText: "我是Form组件界面"
    }
  }

  render() {
    return (
      <div>
        <h1>{this.state.pageText}</h1>
        <NameForm/>
        <hr/>
        <EssayForm/>
        <hr/>
        <Reservation/>
        <hr/>
        <FlavorForm/>
      </div>
    )
  }
}
export default Form