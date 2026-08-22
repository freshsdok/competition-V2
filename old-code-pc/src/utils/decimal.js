import Decimal from 'decimal.js';

// 加法
function add(arg1, arg2) {
	return Number(Decimal(arg1||0).add(Decimal(arg2||0)))
}

// 减法
function sub(arg1, arg2) {
	return Number(Decimal(arg1||0).sub(Decimal(arg2||0)))
}

// 乘法
function mul(arg1, arg2) {
	return Number(Decimal(arg1||0).mul(Decimal(arg2||0)))
}

// 除法
function div(arg1, arg2) {
	return Number(Decimal(arg1||0).div(Decimal(arg2||0)))
}

export {
	add,
	sub,
	mul,
	div
}