import React from 'react';

class EssayForm extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        value: '请撰写一篇关于你喜欢的 DOM 元素的文章.'
      };
  
      this.handleChange = this.handleChange.bind(this);
      this.handleSubmit = this.handleSubmit.bind(this);
    }
  
    handleChange(event) {
      this.setState({value: event.target.value});
    }
  
    handleSubmit(event) {
      alert('提交的文章: ' + this.state.value);
      // 阻止元素默认发生的行为（例如，当点击提交按钮时阻止对表单的提交），因为表单一提交就会刷新页面吧，这里保持不变
      event.preventDefault();
    }
  
    render() {
      return (
        <form onSubmit={this.handleSubmit}>
          <label>
            文章:
            <textarea value={this.state.value} onChange={this.handleChange} />
          </label>
          <input type="submit" value="提交" />
        </form>
      );
    }
  }
  
  export default EssayForm