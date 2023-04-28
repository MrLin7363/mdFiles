import React from 'react'
import Reflux from 'reflux'
import Action from '../actions/Action';
import Store from '../stores/Store';

class Index extends Reflux.Component {
    constructor(props) {
        super(props);
        this.store = Store;
    }

    render() {
        return (
            <div>
                {this.state.num}
                <button onClick={() => Action.add()}>+</button>
            </div>
        );
    }
}

export default Index;