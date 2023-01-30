#!/usr/bin/env node
import "source-map-support/register";
import * as cdk from "aws-cdk-lib";
import { ToolsSlackBotStack } from "../lib/ToolsBotStack";

const app = new cdk.App();
new ToolsSlackBotStack(app, "DemoStack", {
  stage: "dev",
});
