package com.github.reugn.memento.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

object Serde {

  def serialize(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val os = new ObjectOutputStream(stream)
    os.writeObject(value)
    os.close()
    stream.toByteArray
  }

  def deserialize(bytes: Array[Byte]): Any = {
    val is = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = is.readObject
    is.close()
    value
  }
}
