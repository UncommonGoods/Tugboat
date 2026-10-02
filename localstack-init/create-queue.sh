#!/bin/sh
# Copyright (c) 2025 Uncommon Goods LLC
# SPDX-License-Identifier: MIT
# Runs automatically once LocalStack reports ready (init/ready.d hook).
# Creates the SQS queue that xo binds to via TUGBOAT_QUEUE.
awslocal sqs create-queue --queue-name tugboat-queue
