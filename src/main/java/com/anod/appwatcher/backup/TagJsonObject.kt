package com.anod.appwatcher.backup

import com.anod.appwatcher.model.Tag
import info.anodsplace.framework.json.JsonReader
import info.anodsplace.framework.json.JsonWriter

/**
 * @author algavris
 * @date 27/06/2017
 */
class TagJsonObject(val tag: Tag?) {

    constructor(tag: Tag, writer: JsonWriter) : this(tag) {
        write(tag, writer)
    }

    constructor(reader: JsonReader): this(read(reader))

    companion object {
        fun write(source: Tag, writer: JsonWriter) {
            writer.beginObject()
            writer.name("id").value(source.id.toLong())
            writer.name("name").value(source.name)
            writer.name("color").value(source.color.toLong())
            writer.endObject()
        }

        fun read(reader: JsonReader): Tag? {
            var id = 0
            var color = Tag.DEFAULT_COLOR
            var name = "Tag name"

            reader.beginObject()
            while (reader.hasNext()) {
                val key = reader.nextName()
                when (key) {
                    "id" -> id = reader.nextInt()
                    "name" -> name = reader.nextString()
                    "color" -> color = reader.nextInt()
                }
            }
            reader.endObject()

            if (id > 0) {
                return Tag(id, name, color)
            }
            return null
        }
    }
}