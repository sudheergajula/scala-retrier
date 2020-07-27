package com.retry.core

sealed trait BackOffStrategy {
  def calculateDelay(attempt: Int, duration: Long): Long
}

object LinerBackOffStrategy extends BackOffStrategy {
  override def calculateDelay(attempt: Int, duration: Long): Long = duration * attempt
}


object ExponentialBackOffStrategy extends BackOffStrategy {
  override def calculateDelay(attempt: Int, duration: Long): Long = duration ^ attempt
}

object FixedDelayBackOffStrategy extends BackOffStrategy {
  override def calculateDelay(attempt: Int, duration: Long): Long = duration
}