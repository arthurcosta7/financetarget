import { GoalDetail } from "@/components/GoalDetail";

export default async function GoalPage({ params }: { params: Promise<{ goalId: string }> }) {
  const { goalId } = await params;
  return <GoalDetail goalId={goalId} />;
}
