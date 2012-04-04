
var operators = [
       "airtel", "reliance", "tata", "idea", "bsnl", "idea", "undefined"
    ];

var thread = ctx.getThreadNum();
var thread_count = ctx.getThreadGroup().getNumThreads();

function convertToInt(value) {
    value = parseInt(value);
    if (isNaN(value)) {
        value = 0;
    }
    return value;
}

function fetch_from_vars_as_int(key) {
    var value = vars.get(key);
    value = convertToInt(value);
    return value;
}

var operator_counter = fetch_from_vars_as_int("operator_counter");
var user_counter = fetch_from_vars_as_int("user_counter");

if ((user_counter + thread) > 21500) {
    user_counter = 0;
    operator_counter = operator_counter + 1;
    if (operator_counter > 6) {
        operator_counter = 0;
    }
}

var user = user_counter + thread;
vars.put("user_counter", user_counter + thread_count);
vars.put("operator_counter", operator_counter);

var callerId = operator_counter + "" + user
var callId = callerId + new Date().valueOf();

vars.put("callId", callId);
vars.put("callerId", callerId);
vars.put("operator", operators[operator_counter]);
vars.put("thread", thread);