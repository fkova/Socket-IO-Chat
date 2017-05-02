var mongoose = require("mongoose");
var dblink='mongodb://localhost/chat';

if(process.env.OPENSHIFT_MONGODB_DB_PASSWORD){
    dblink = process.env.OPENSHIFT_MONGODB_DB_USERNAME + ":" +
        process.env.OPENSHIFT_MONGODB_DB_PASSWORD + "@" +
        process.env.OPENSHIFT_MONGODB_DB_HOST + ':' +
        process.env.OPENSHIFT_MONGODB_DB_PORT + '/' +
        process.env.OPENSHIFT_APP_NAME;
}

mongoose.connect(dblink,{server:{auto_reconnect:false}},function (err) {
    if (err){
        console.log(err);
    }else{
        console.log('Conntected to mongodb/chat');
    }
});

var chatSchema = mongoose.Schema({
    ip: String,
    username: String,
    message: String,
    socId: String,
    created: {type: Date, default: Date.now}
});

var Chat = mongoose.model('Message',chatSchema);

exports.tbMsg= Chat;


