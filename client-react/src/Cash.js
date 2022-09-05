function Cash(props) {
	const { portfolio } = props;

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Cash</div>
			</div>
			<table>
				<tbody>
					<tr><td className="l">Cash</td><td>{portfolio.cash.toFixed(2)}</td></tr>
					<tr><td className="l">Cash on hold</td><td>- {portfolio.cashOnHold.toFixed(2)}</td></tr>
					<tr style={{ borderTop: "1px solid grey" }}><td className="l">Available cash</td><td>{portfolio.cashAvailable.toFixed(2)}</td></tr>
				</tbody>
			</table>
		</div>
	);
}

export default Cash;
