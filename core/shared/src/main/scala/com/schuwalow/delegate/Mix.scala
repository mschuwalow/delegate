package com.schuwalow.delegate

import scala.language.experimental.macros

/**
 * evidence that two instances can be mixed.
 * Should normally be generated by macros.
 */
trait Mix[A, B] {

  def mix(a: A, b: B): A with B

}

object Mix {

  def apply[A, B](implicit ev: Mix[A, B]) = ev

  implicit def deriveMix[A, B]: Mix[A, B] = macro Macros.mixImpl[A, B]

}
