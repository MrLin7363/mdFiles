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
        // fetch("http://127.0.0.1:8003/vuln/redis/api/v1/lin/list")
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
                        userlist: result.body,
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
                <ul>
                    <li key="head">
                        Name Savings Job
                    </li>
                    {/*调式技巧：打印为表格的方式 */}
                    {console.table(userlist)}
                    {userlist.map(item => (
                        <li key={item.name}>
                            {item.name} {item.age} {item.job}
                        </li>
                    ))}
                </ul>
            )
        } else {
            return (
                <div>请求未成功</div>
            )
        }
    }
}
export default MyTableList