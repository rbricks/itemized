package io.rbricks.itemized.circeinstances

import io.circe._
import io.circe.Decoder.Result
import io.rbricks.itemized._

trait VerbatimCirceInstances {

  private val stringDecoder = implicitly[Decoder[String]]

  implicit def circeEncoderForItemized[T <: Itemized](implicit
    itemizedCodec: ItemizedCodec[T]): Encoder[T] =
    new Encoder[T] {
      def apply(value: T): Json = Json.fromString(itemizedCodec.toRep(value))
    }

  implicit def circeDecoderForItemized[T <: Itemized](implicit
    itemizedCodec: ItemizedCodec[T],
    classTag: scala.reflect.ClassTag[T]): Decoder[T] =
      stringDecoder.emap { s =>
        itemizedCodec.fromRep(s) match {
          case Some(x) => Right(x)
          case None =>
            Left(s"$s is not a member of ${classTag.runtimeClass.getName}")
        }
      }
}

