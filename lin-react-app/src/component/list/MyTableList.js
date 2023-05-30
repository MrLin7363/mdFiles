import React from 'react';
import { getUserList } from '../../api/api';

class MyTableList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isLoaded: false,
            userlist: []
        }
    }
    componentDidMount() {
        // 原生fetch方式调后端
        // fetch("http://127.0.0.1:8003/test/v1/lin/list")
        //     .then(res => res.json())
        //     .then(
        //         (result) => {
        //             this.setState({
        //                 userlist: result.body,
        //                 isLoaded: true
        //             });
        //         },
        //         (error) => {
        //             console.log(error);
        //         }
        //     )
        getUserList()
            .then( 
                (result) => {
                    this.setState({
                        userlist: result,
                        isLoaded: true
                    });
                },
                (error) => {
                    console.log(error);
                }
            )
    }

    render() {
        const { isLoaded, userlist } = this.state;
        if (isLoaded) {
            return (
                <div>
                <ul>
                    <li key="head">
                        Name Savings Job
                    </li>
                    {/*调式技巧：打印为表格的方式 */}
                    {console.table(userlist)}
                    {/* 请求成功的时候才是array,否则不算 */}
                    {console.log(Array.isArray(userlist))}
                    {/* 先判断是不是array,再map输出；或者只是  userlist&&Object.values(userlist).map() */}
                    {console.log(Array.isArray(Object.values(userlist)))}
                    {Array.isArray(userlist)&&userlist.map((item, index) => (
                        <li key={index}>
                            {item.proxySystem} {item.healthDesc} {item.request12hour}
                        </li>
                    ))}
                </ul>
                </div>
            )
        } else {
            return (
                <div>请求未成功</div>
            )
        }
    }
}
export default MyTableList