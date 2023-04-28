import React from 'react';
import styled from "styled-components";

const Wrap = styled.div`
    height: 100%;
    display: flex;
    .hidden{
        display:none;
    }
`;
class BaiduIframe extends React.Component {

    render () {
        // calc() 计算函数 + - * / 
        return (<Wrap style={{height: 'calc(100% - 51px)'}}>
            <iframe src='https://www.baidu.com/'
                    width={'100%'} frameBorder={0}
                    title={'unknown'}
            />
        </Wrap>)
    }
}

export default BaiduIframe