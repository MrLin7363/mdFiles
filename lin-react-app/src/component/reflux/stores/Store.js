import Reflux from 'reflux'
import Action from '../actions/Action';

// 可以抽离出公共的Store,后期组件多的时候，可以多个组件从这里取state
class Store extends Reflux.Store {
    constructor() {
        super();
        this.listenables = Action;
        this.state = {
            num: 0
        }
    }

    // 根据actions中定义的  on+大写开头
    onAdd() {
        this.setState({num: this.state.num + 1});
    }
}

export default Store;