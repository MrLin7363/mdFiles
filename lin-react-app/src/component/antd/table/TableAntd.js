import React from 'react';
import { Table, DatePicker, Spin } from 'antd'
import dayjs from 'dayjs';
import { getUriList } from '../../../api/api';

const { RangePicker } = DatePicker

const data = {
  system: 'TEST',
  proxyUri: '/',
  beginTime: 1685171666567,
  endTime: 1685172266567,
}
const data2 = {
  system: 'TEST',
  proxyUri: '/test/api',
  beginTime: 1685171666567,
  endTime: 1685172266567,
}

class TableAntd extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      configList: [data, data2],
      searchTime: [dayjs().subtract(10, 'minutes'), dayjs()],
      uriList: []
    }
    // this.onTimeChange = this.onTimeChange.bind(this)   使用箭头函数传参自动会把this带进去函数中 1.bind 2.函数传参 3.箭头函数自动传 4. 方法写成 箭头方法()=>{}
  }

  // 只会调一次，在第一次组件渲染完后，setState会更新组件显示
  componentDidMount() {
    console.log("componentDidMount");
    this.getSingleDetail();
  }

  getSingleDetail() {
    let { uriList, searchTime, configList } = this.state;
    // uriList = configList.map(item => { return item.proxyUri })  这个是返回一个字符串列表 ["/","/test"]
    // 初始化左侧第一列的URI，右侧的几列数据分行调接口取
    uriList = configList.map(item => { return { proxyUri: item.proxyUri } })  // 这个是返回一个对象列表 [{uri:"/"},{uri:"/test"}]
    this.setState({ uriList });
    configList.map((item, i) => {
      const requestDate = {
        system: item.system,
        proxyUri: item.proxyUri,
        beginTime: searchTime[0].valueOf(),
        endTime: searchTime[1].valueOf(),
      }
      getUriList(requestDate).then(
        (result) => {
          item = { ...item, ...result }
          uriList[i] = item;
          this.setState({ uriList })
        }, (error) => {
          console.log(error);
        }
      )
    });
  }

  onTimeChange(dates, dataString) {
    console.log("onTimeChange")
    console.log(dataString) //  ['2023-05-27 15:56:03', '2023-05-27 23:06:03']
    this.setState({
      searchTime: dates
    })
    setTimeout(() => {
      this.getSingleDetail();
    }, 100)
  }

  render() {
    console.log("render" + dayjs('2023-05-27 15:00:00', 'YYYY-MM-DD HH:mm:ss'))
    let { searchTime, uriList } = this.state
    { console.table(uriList) }

    // 指针改变了，Table组件中的dataSource也会有变化，Table组件会重新渲染！否则默认数据无变化不重新渲染
    const uriListDisPlay = uriList ? [...uriList] : [];
    const columns = [
      {
        title: 'URI',
        dataIndex: 'proxyUri',
        key: 'proxyUri',
        render: (value) => <span>{value}</span>,
      },
      {
        title: '请求量',
        dataIndex: 'request',
        key: 'request',
        render: renderLoadingText,
      },
      {
        title: '成功率',
        dataIndex: 'successRate',
        key: 'successRate',
        render: renderLoadingText,
      }
    ]
    return (
      <div>
        <text>初始化左侧第一列的URI，右侧的几列数据分行调接口取,默认是十分钟</text>
        <br />
        <RangePicker
          // 默认值
          defaultValue={searchTime}
          // 这个是时间选择框选择的时候时间的默认值，不是日期选择框的默认值
          showTime={{
            defaultValue: [dayjs().subtract(10, 'minutes'), dayjs()],
          }}
          presets={[
            { label: 'Last 10 minutes', value: [dayjs().subtract(10, 'minutes'), dayjs()] },
            { label: 'Last 10 hours', value: [dayjs().subtract(10, 'hours'), dayjs()] },
            { label: 'Last 1 months', value: [dayjs().subtract(1, 'months'), dayjs()] },
          ]}
          onChange={(dates, dateString) => this.onTimeChange(dates, dateString)}
        // onChange={this.onTimeChange}  未使用箭头函数，方法中的this无法获取，1.bind 2.函数传参 3.箭头函数自动传
        />
        {/* 这里只拿到第一次更新的uriList */}
        <Table
          rowKey={record => record.proxyUri}
          columns={columns}
          dataSource={uriListDisPlay}
          pagination={false}
        />
      </div>
    );
  }
}

function renderLoadingText(value) {
  console.log(value)
  // 如果接口未返回就显示刷新按钮
  if (value === undefined) {
    return <Spin size="small" spinning={true} />
  }
  return <span>{value}</span>
}

export default TableAntd