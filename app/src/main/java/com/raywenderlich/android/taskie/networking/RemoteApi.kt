package com.raywenderlich.android.taskie.networking

import com.google.gson.Gson
import com.raywenderlich.android.taskie.App
import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.UserProfile
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.GetTasksResponse
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi(private val remoteApiService: RemoteApiService) {

  private val gson = Gson()

  fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
    Thread(Runnable {
      val connection = URL("$BASE_URL/api/login").openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("Accept", "application/json")
      connection.readTimeout = 10000
      connection.connectTimeout = 10000
      connection.doOutput = true
      connection.doInput = true

      val body = gson.toJson(userDataRequest)

      val bytes = body.toByteArray()

      try {
        connection.outputStream.use { outputStream ->
          outputStream.write(bytes)
        }

        val reader = InputStreamReader(connection.inputStream)

        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.useLines { lines ->
            lines.forEach {
              response.append(it.trim())
            }
          }

          val jsonObject = JSONObject(response.toString())

          onUserLoggedIn(jsonObject.getString("token"), null)
        }
      } catch (error: Throwable) {
        onUserLoggedIn(null, error)
      }

      connection.disconnect()
    }).start()
  }

  fun registerUser(userDataRequest: UserDataRequest, onUserCreated: (String?, Throwable?) -> Unit) {
    Thread(Runnable {
      val connection = URL("$BASE_URL/api/register").openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("Accept", "application/json")
      connection.readTimeout = 10000
      connection.connectTimeout = 10000
      connection.doOutput = true
      connection.doInput = true

      val body = gson.toJson(userDataRequest)

      val bytes = body.toByteArray()

      try {
        connection.outputStream.use { outputStream ->
          outputStream.write(bytes)
        }

        val reader = InputStreamReader(connection.inputStream)

        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.useLines { lines ->
            lines.forEach {
              response.append(it.trim())
            }
          }

          val jsonObject = JSONObject(response.toString())

          onUserCreated(jsonObject.getString("message"), null)
        }
      } catch (error: Throwable) {
        onUserCreated(null, error)
      }

      connection.disconnect()
    }).start()
  }

  fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
    Thread(Runnable {
      val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
      connection.requestMethod = "GET"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("Accept", "application/json")
      connection.setRequestProperty("Authorization", App.getToken())
      connection.readTimeout = 10000
      connection.connectTimeout = 10000
      connection.doInput = true

      try {
        val reader = InputStreamReader(connection.inputStream)

        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.useLines { lines ->
            lines.forEach {
              response.append(it.trim())
            }
          }

          val tasksResponse = gson.fromJson(response.toString(), GetTasksResponse::class.java)
          val unfinishedTasks = tasksResponse.notes.filter { !it.isCompleted }

          onTasksReceived(unfinishedTasks, null)
        }
      } catch (error: Throwable) {
        onTasksReceived(emptyList(), error)
      }

      connection.disconnect()
    }).start()
  }

  fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
    onTaskDeleted(null)
  }

  fun completeTask(taskId: String, onTaskCompleted: (Throwable?) -> Unit) {
    Thread(Runnable {
      val connection = URL(
          "$BASE_URL/api/note/complete?id=$taskId"
      ).openConnection() as HttpURLConnection

      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("Accept", "application/json")
      connection.setRequestProperty("Authorization", App.getToken())
      connection.readTimeout = 10000
      connection.connectTimeout = 10000
      connection.doOutput = true
      connection.doInput = true

      try {
        val reader = InputStreamReader(connection.inputStream)

        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.useLines { lines ->
            lines.forEach {
              response.append(it.trim())
            }
          }

          onTaskCompleted(null)
        }
      } catch (error: Throwable) {
        onTaskCompleted(error)
      }

      connection.disconnect()
    }).start()
  }

  fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {
    Thread(Runnable {
      val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("Accept", "application/json")
      connection.setRequestProperty("Authorization", App.getToken())
      connection.readTimeout = 10000
      connection.connectTimeout = 10000
      connection.doOutput = true
      connection.doInput = true

      val request = gson.toJson(addTaskRequest)

      try {
        connection.outputStream.use { outputStream ->
          outputStream.write(request.toString().toByteArray())
        }

        val reader = InputStreamReader(connection.inputStream)

        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.useLines { lines ->
            lines.forEach {
              response.append(it.trim())
            }
          }

          val task = gson.fromJson(response.toString(), Task::class.java)

          onTaskCreated(task, null)
        }
      } catch (error: Throwable) {
        onTaskCreated(null, error)
      }

      connection.disconnect()
    }).start()
  }

  fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
    onUserProfileReceived(UserProfile("mail@mail.com", "Filip", 10), null)
  }
}