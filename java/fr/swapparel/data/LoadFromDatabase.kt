package fr.swapparel.data

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import fr.swapparel.extensions.*


class LoadFromDatabase {
    private var mDb: SavedApparelDB? = null
    private val mUiHandler = Handler()

    fun loadApparel(context: Context, apparelRetriever: ApparelLoader) {
        //Get thread ready to load database
        val mDbWorkerThread = HandlerThread("snappaThread")
        mDbWorkerThread.start()
        val looper = mDbWorkerThread.looper
        val handler = Handler(looper)

        mDb = SavedApparelDB.getInstance(context)

        handler.post {
            val apparelData = mDb?.apparelDataDao()?.getAll()
            mUiHandler.post {
                apparelRetriever.onApparelReceived(bindDataToList(apparelData))
                mDbWorkerThread.quit()
            }
        }
    }

    fun loadRawData(context: Context, apparelRetriever: ApparelRawDataLoader) {
        //Get thread ready to load database
        val mDbWorkerThread = HandlerThread("snappaThread")
        mDbWorkerThread.start()
        val looper = mDbWorkerThread.looper
        val handler = Handler(looper)

        mDb = SavedApparelDB.getInstance(context)

        handler.post {
            val apparelData = mDb?.apparelDataDao()?.getAll()
            mUiHandler.post {
                apparelRetriever.onDataReceived(apparelData!!)
                mDbWorkerThread.quit()
            }
        }
    }

    private fun bindDataToList(apparel: List<SavedApparel>?): List<Apparel> {
        val usr = mutableListOf<Apparel>()
        apparel?.forEach {
            usr.add(
                Apparel(
                    it.type,
                    it.drawablePath,
                    it.color,
                    it.heaviness,
                    ColorMatching.matchPinterest(it.color),
                    0.0
                )
            )
        }
        return usr
    }

    fun update(apparel: SavedApparel, context : Context) {
        //Get thread ready to load database
        val mDbWorkerThread = HandlerThread("snappaThreadAdda")
        mDbWorkerThread.start()
        val looper = mDbWorkerThread.looper
        val handler = Handler(looper)

        mDb = SavedApparelDB.getInstance(context)

        handler.post {
            mDb?.apparelDataDao()?.updateApparel(apparel)
            mUiHandler.post {
                mDbWorkerThread.quit()
            }
        }
    }
}