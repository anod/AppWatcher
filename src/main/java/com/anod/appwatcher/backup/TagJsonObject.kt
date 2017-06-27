package com.anod.appwatcher.backup

import com.android.util.JsonReader
import com.android.util.JsonWriter
import com.anod.appwatcher.model.Tag

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
                if (key == "id") {
                    id = reader.nextInt()
                } else if (key == "name") {
                    name = reader.nextString()
                } else if (key == "color") {
                    color = reader.nextInt()
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