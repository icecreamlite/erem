package api

object SocketData {
    private lateinit var onMsgChangeListener: SocketDataListener
    private lateinit var msg: String

    fun setOnMsgChangeListener(_onMsgChangeListener: SocketDataListener) {
        onMsgChangeListener = _onMsgChangeListener
    }

    fun getFanAutoMsg(): String {
        return msg
    }

    fun setFanAutoMsg(_msg: String) {
        msg = _msg
        onMsgChangeListener.onFanAuto(msg)
    }

}