export const actionTypes = {
    INCREMENT: 'increment',
    DECREMENT: 'decrement',
}

export const increament=(data)=>{
    return {
        type:actionTypes.INCREMENT,
        data:data
    }
}
export const decrement=(data)=>{
    return {
        type:actionTypes.DECREMENT,
        data:data
    }
}