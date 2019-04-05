package org.flaxo.plagiarism.support

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import org.flaxo.plagiarism.model.Direction

/**
 * Deserializer for the [Direction] class
 */
@Serializer(forClass = Direction::class)
object DirectionSerializer : KSerializer<Direction> {

    override fun deserialize(input: Decoder): Direction =
            Direction.valueOf(input.decodeString().toUpperCase())

    override fun serialize(output: Encoder, obj: Direction) =
            output.encodeString(obj.toString())

}